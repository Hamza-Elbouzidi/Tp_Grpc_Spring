import 'package:flutter/material.dart';
import 'gRPCService.dart';
import 'src/generated/CompteService.pb.dart';

void main() {
  runApp(GrpcClientApp());
}

class GrpcClientApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'gRPC Client',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: GrpcHomePage(),
    );
  }
}

class GrpcHomePage extends StatefulWidget {
  @override
  _GrpcHomePageState createState() => _GrpcHomePageState();
}

class _GrpcHomePageState extends State<GrpcHomePage> {
  final GrpcService grpcService = GrpcService();
  late Future<List<Compte>> comptes;
  late Future<GetTotalSoldeResponse> soldeStats;
  final TextEditingController _idController = TextEditingController();
  final TextEditingController _soldeController = TextEditingController();
  final TextEditingController _dateCreationController = TextEditingController();
  TypeCompte _type = TypeCompte.COURANT;

  @override
  void initState() {
    super.initState();
    grpcService.createChannel().then((_) {
      refreshComptes();
      soldeStats = grpcService.getTotalSolde();
    });
  }

  void refreshComptes() {
    setState(() {
      comptes = grpcService.getAllComptes();
    });
  }

  @override
  void dispose() {
    grpcService.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("gRPC Client")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: SingleChildScrollView(
          child: Column(
            children: [
              _buildAddAccountSection(),
              Divider(),
              _buildAllComptesSection(),
              Divider(),
              _buildCompteByIdSection(),
              Divider(),
              _buildDeleteCompteSection(),
              Divider(),
              _buildFilterByTypeSection(),
              Divider(),
              _buildTotalSoldeSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAddAccountSection() {
    return Column(
      children: [
        Text('Add New Account', style: TextStyle(fontSize: 18)),
        TextField(controller: _soldeController, decoration: InputDecoration(labelText: "Solde")),
        TextField(controller: _dateCreationController, decoration: InputDecoration(labelText: "Date Creation (YYYY-MM-DD)")),
        DropdownButtonFormField<TypeCompte>(
          value: _type,
          onChanged: (TypeCompte? newValue) => setState(() => _type = newValue!),
          items: TypeCompte.values.map((type) => DropdownMenuItem(value: type, child: Text(type.toString()))).toList(),
        ),
        ElevatedButton(
          onPressed: () {
            final compte = CompteRequest()
              ..solde = double.parse(_soldeController.text)
              ..dateCreation = _dateCreationController.text
              ..type = _type;

            grpcService.saveCompte(compte).then((_) => refreshComptes());
          },
          child: Text("Add Account"),
        ),
      ],
    );
  }

  Widget _buildAllComptesSection() {
    return Column(
      children: [
        Text('All Comptes', style: TextStyle(fontSize: 18)),
        FutureBuilder<List<Compte>>(
          future: comptes,
          builder: (context, snapshot) {
            if (!snapshot.hasData) return CircularProgressIndicator();
            return ListView.builder(
              shrinkWrap: true,
              physics: NeverScrollableScrollPhysics(),
              itemCount: snapshot.data!.length,
              itemBuilder: (context, index) {
                final compte = snapshot.data![index];
                return Card(
                  child: ListTile(
                    title: Text('Compte ID: ${compte.id}'),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Solde: ${compte.solde}'),
                        Text('Date Creation: ${compte.dateCreation}'),
                        Text('Type: ${compte.type.toString().split('.').last}'),
                      ],
                    ),
                  ),
                );
              },
            );
          },
        ),
      ],
    );
  }

  Widget _buildCompteByIdSection() {
    return Column(
      children: [
        Text('Get Compte by ID'),
        TextField(controller: _idController, decoration: InputDecoration(labelText: "ID")),
        ElevatedButton(
          onPressed: () {
            grpcService.getCompteById(int.parse(_idController.text)).then((compte) {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  title: Text('Compte Found'),
                  content: Text('ID: ${compte.id}\nSolde: ${compte.solde}\nDate: ${compte.dateCreation}\nType: ${compte.type}'),
                ),
              );
            });
          },
          child: Text("Get Compte by ID"),
        ),
      ],
    );
  }

  Widget _buildDeleteCompteSection() {
    return Column(
      children: [
        Text('Delete Compte by ID'),
        TextField(controller: _idController, decoration: InputDecoration(labelText: "ID")),
        ElevatedButton(
          onPressed: () {
            grpcService.deleteCompte(int.parse(_idController.text)).then((message) {
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
              refreshComptes();
            });
          },
          child: Text("Delete Compte"),
        ),
      ],
    );
  }

  Widget _buildFilterByTypeSection() {
    return Column(
      children: [
        Text('Filter Comptes by Type'),
        DropdownButtonFormField<TypeCompte>(
          value: _type,
          onChanged: (TypeCompte? newValue) => setState(() => _type = newValue!),
          items: TypeCompte.values.map((type) => DropdownMenuItem(value: type, child: Text(type.toString()))).toList(),
        ),
        ElevatedButton(
          onPressed: () => setState(() => comptes = grpcService.getComptesByType(_type)),
          child: Text("Filter by Type"),
        ),
      ],
    );
  }

  Widget _buildTotalSoldeSection() {
    return Column(
      children: [
        Text('Total Solde', style: TextStyle(fontSize: 18)),
        FutureBuilder<GetTotalSoldeResponse>(
          future: soldeStats,
          builder: (context, snapshot) {
            if (!snapshot.hasData) return CircularProgressIndicator();
            final stats = snapshot.data!.stats;
            return Text('Total: ${stats.sum}, Avg: ${stats.average}');
          },
        ),
      ],
    );
  }
}
